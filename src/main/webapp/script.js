document.addEventListener('DOMContentLoaded', function() {
    // Initialize search functionality
    const queryInput = document.getElementById('search-input');
    const queryButton = document.getElementById('search-button');

    queryButton.addEventListener('click', performSearch);
    queryInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            performSearch();
        }
    });

    async function performSearch() {
        const searchText = queryInput.value.trim();
        if (searchText) {
            try {
                // Get API key
                const keyResponse = await fetch('/getGoogleAPIKey');
                const apiKey = await keyResponse.text();

                // Fetch available locations
                const locationsResponse = await fetch('/api/locations');
                const locationsList = await locationsResponse.json();
                
                // Filter locations based on query
                const matchingLocations = locationsList.filter(loc => 
                    loc.name.toLowerCase().includes(searchText.toLowerCase()) ||
                    loc.address.toLowerCase().includes(searchText.toLowerCase())
                );

                if (matchingLocations.length > 0) {
                    // Navigate to map with search results
                    window.location.href = `#map?search=${encodeURIComponent(searchText)}`;
                } else {
                    // Try to geocode the address if no matching locations
                    const geocodeResponse = await fetch(`/locationSelectBackend?address=${encodeURIComponent(searchText)}`);
                    const locationCoords = await geocodeResponse.text();
                    
                    if (locationCoords && locationCoords !== "Address not found") {
                        window.location.href = `#map?coordinates=${locationCoords}`;
                    } else {
                        alert('No locations found matching your search.');
                    }
                }
            } catch (error) {
                console.error('Search failed:', error);
                alert('An error occurred during the search. Please try again.');
            }
        }
    }

    // Implement smooth scrolling navigation
    document.querySelectorAll('a[href^="#"]').forEach(link => {
        link.addEventListener('click', function(e) {
            e.preventDefault();
            const targetElement = document.querySelector(this.getAttribute('href'));
            if (targetElement) {
                targetElement.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    // Add header scroll effect
    let previousScroll = 0;
    const headerElement = document.querySelector('header');

    window.addEventListener('scroll', function() {
        const currentScroll = window.pageYOffset;
        
        if (currentScroll <= 0) {
            headerElement.classList.remove('scroll-up');
            return;
        }
        
        if (currentScroll > previousScroll && !headerElement.classList.contains('scroll-down')) {
            // Scrolling down
            headerElement.classList.remove('scroll-up');
            headerElement.classList.add('scroll-down');
        } else if (currentScroll < previousScroll && headerElement.classList.contains('scroll-down')) {
            // Scrolling up
            headerElement.classList.remove('scroll-down');
            headerElement.classList.add('scroll-up');
        }
        previousScroll = currentScroll;
    });

    // Setup WebSocket for real-time updates
    const socket = new WebSocket('ws://' + window.location.host + '/ws');
    
    socket.onopen = function() {
        console.log('WebSocket connection established');
    };
    
    socket.onmessage = function(event) {
        console.log('Received message:', event.data);
        // Handle real-time updates here
    };
    
    socket.onclose = function() {
        console.log('WebSocket connection closed');
    };
    
    socket.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
});