// Real-time bidding functionality using WebSocket

let stompClient = null;
let auctionId = null;

function initBidding(auctionIdParam) {
    auctionId = auctionIdParam;
    
    // Connect to WebSocket
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    
    // Disable debug logging (optional - remove if you want to see STOMP frames)
    stompClient.debug = function(str) {
        // Uncomment to see WebSocket debug messages
        // console.log('STOMP: ' + str);
    };
    
    stompClient.connect({}, function(frame) {
        console.log('WebSocket connected for auction: ' + auctionId);
        
        // Subscribe to auction updates
        stompClient.subscribe('/topic/auction/' + auctionId, function(bidResponse) {
            try {
                const bid = JSON.parse(bidResponse.body);
                updateBidDisplay(bid);
            } catch (error) {
                console.error('Error parsing bid update:', error);
            }
        });
    }, function(error) {
        console.error('WebSocket connection error:', error);
        // Attempt to reconnect after 5 seconds
        setTimeout(function() {
            console.log('Attempting to reconnect WebSocket...');
            initBidding(auctionId);
        }, 5000);
    });
    
    // Handle bid form submission
    const bidForm = document.getElementById('bidForm');
    if (bidForm) {
        bidForm.addEventListener('submit', function(e) {
            e.preventDefault();
            placeBid();
        });
    }
}

function placeBid() {
    const bidAmountInput = document.getElementById('bidAmountInput');
    const bidAmount = parseFloat(bidAmountInput.value);
    
    if (!bidAmount || bidAmount <= 0) {
        alert('Please enter a valid bid amount');
        return;
    }
    
    // Get current highest bid to validate
    const currentBidElement = document.getElementById('bidAmount');
    const currentBid = currentBidElement ? parseFloat(currentBidElement.textContent) : 0;
    
    if (bidAmount <= currentBid) {
        alert('Bid must be higher than current highest bid: $' + currentBid.toFixed(2));
        return;
    }
    
    const bidDTO = {
        auctionId: parseInt(auctionId),
        bidAmount: bidAmount
    };
    
    // Disable the form while submitting
    const bidForm = document.getElementById('bidForm');
    const submitButton = bidForm ? bidForm.querySelector('button[type="submit"]') : null;
    if (submitButton) {
        submitButton.disabled = true;
        submitButton.textContent = 'Placing Bid...';
    }
    
    fetch('/bid/place', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(bidDTO)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            bidAmountInput.value = '';
            // Show success message briefly
            if (submitButton) {
                const originalText = submitButton.textContent;
                submitButton.textContent = 'Bid Placed!';
                setTimeout(function() {
                    submitButton.textContent = originalText;
                }, 2000);
            }
            // The WebSocket will update the display automatically
        } else {
            alert('Error: ' + data.message);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Error placing bid. Please try again.');
    })
    .finally(() => {
        // Re-enable the form
        if (submitButton) {
            submitButton.disabled = false;
            if (submitButton.textContent === 'Placing Bid...') {
                submitButton.textContent = 'Place Bid';
            }
        }
    });
}

function getCsrfToken() {
    // Try to get from meta tag
    const metaTag = document.querySelector('meta[name="_csrf"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }
    
    // Try to get from cookie (Spring Security uses XSRF-TOKEN)
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            return decodeURIComponent(value);
        }
    }
    
    return '';
}

function getCsrfHeaderName() {
    const metaTag = document.querySelector('meta[name="_csrf_header"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }
    return 'X-XSRF-TOKEN';
}

function updateBidDisplay(bid) {
    // Parse bid amount (handle both number and string from BigDecimal)
    let bidAmount = bid.bidAmount;
    if (typeof bidAmount === 'string') {
        bidAmount = parseFloat(bidAmount);
    }
    
    // Update current highest bid amount display
    const bidAmountElement = document.getElementById('bidAmount');
    if (bidAmountElement) {
        bidAmountElement.textContent = bidAmount.toFixed(2);
    }
    
    // Update the current bid section text
    const currentBidSection = document.getElementById('currentBid');
    if (currentBidSection) {
        const span = currentBidSection.querySelector('span');
        if (span) {
            span.textContent = bidAmount.toFixed(2);
        }
    }
    
    // Update minimum bid input field
    const bidAmountInput = document.getElementById('bidAmountInput');
    if (bidAmountInput) {
        // Set minimum to be slightly higher than current bid
        const minBid = (bidAmount + 0.01).toFixed(2);
        bidAmountInput.setAttribute('min', minBid);
        bidAmountInput.setAttribute('placeholder', `Minimum: $${minBid}`);
    }
    
    // Add bid to bids list (only if not already present)
    const bidsList = document.getElementById('bidsList');
    if (bidsList) {
        // Check if this bid already exists in the list
        const existingBids = bidsList.querySelectorAll('.bid-item');
        let bidExists = false;
        existingBids.forEach(item => {
            if (item.dataset.bidId === bid.id.toString()) {
                bidExists = true;
            }
        });
        
        if (!bidExists) {
            const bidItem = document.createElement('div');
            bidItem.className = 'bid-item';
            bidItem.dataset.bidId = bid.id;
            
            // Format bid time
            let bidTimeStr = '';
            if (bid.bidTime) {
                try {
                    // Handle different date formats
                    const bidTime = typeof bid.bidTime === 'string' ? new Date(bid.bidTime) : bid.bidTime;
                    bidTimeStr = new Date(bidTime).toLocaleString();
                } catch (e) {
                    bidTimeStr = 'Just now';
                }
            } else {
                bidTimeStr = 'Just now';
            }
            
            bidItem.innerHTML = `
                <p><strong>${bid.bidderName}</strong> - 
                   $${bidAmount.toFixed(2)} - 
                   ${bidTimeStr}</p>
            `;
            bidsList.insertBefore(bidItem, bidsList.firstChild);
        }
    }
}

