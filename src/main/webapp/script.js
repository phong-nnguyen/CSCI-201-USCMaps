document.addEventListener('DOMContentLoaded', function() {
    // Check login status immediately when page loads
    checkLoginStatus();
    
    // Search functionality
    const searchInput = document.getElementById('search-input');
    const searchButton = document.getElementById('search-button');

    if (searchInput && searchButton) {
        searchButton.addEventListener('click', handleSearch);
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                handleSearch();
            }
        });
    }

    async function handleSearch() {
        const query = searchInput.value.trim();
        if (query) {
            try {
                // First, get the Google Maps API key
                const apiKeyResponse = await fetch('/getGoogleAPIKey');
                const apiKey = await apiKeyResponse.text();

                // Then search for locations
                const response = await fetch('/api/locations');
                const locations = await response.json();
                
                // Filter locations based on search query
                const filteredLocations = locations.filter(location => 
                    location.name.toLowerCase().includes(query.toLowerCase()) ||
                    location.address.toLowerCase().includes(query.toLowerCase())
                );

                if (filteredLocations.length > 0) {
                    // Redirect to map page with search results
                    window.location.href = `#map?search=${encodeURIComponent(query)}`;
                } else {
                    // If no results found, try to get coordinates for the address
                    const locationResponse = await fetch(`/locationSelectBackend?address=${encodeURIComponent(query)}`);
                    const coordinates = await locationResponse.text();
                    
                    if (coordinates && coordinates !== "Address not found") {
                        window.location.href = `#map?coordinates=${coordinates}`;
                    } else {
                        alert('No locations found matching your search.');
                    }
                }
            } catch (error) {
                console.error('Error during search:', error);
                alert('An error occurred during the search. Please try again.');
            }
        }
    }

    // Login status and logout functionality
    function checkLoginStatus() {
        console.log('Checking login status...');
        const loggedInUser = localStorage.getItem('loggedInUser');
        console.log('Logged in user:', loggedInUser);
        
        const authLinks = document.querySelectorAll('.auth-links');
        const userWelcome = document.querySelector('.user-welcome');
        const logoutLink = document.querySelector('.logout-link');
        const usernameSpan = document.getElementById('username');
        
        if (loggedInUser) {
            // User is logged in
            console.log('User is logged in as:', loggedInUser);
            if (authLinks) authLinks.forEach(link => link.style.display = 'none');
            if (userWelcome) userWelcome.style.display = 'inline-block';
            if (logoutLink) logoutLink.style.display = 'inline-block';
            if (usernameSpan) usernameSpan.textContent = loggedInUser;
            
            // Set up logout functionality
            const logoutBtn = document.getElementById('logout-btn');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', function(e) {
                    e.preventDefault();
                    localStorage.removeItem('loggedInUser');
                    window.location.reload();
                });
            }
        } else {
            // User is not logged in
            console.log('User is not logged in');
            if (authLinks) authLinks.forEach(link => link.style.display = 'inline-block');
            if (userWelcome) userWelcome.style.display = 'none';
            if (logoutLink) logoutLink.style.display = 'none';
        }
    }

    // Smooth scrolling for navigation links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth'
                });
            }
        });
    });

    // Add scroll effect to header
    let lastScroll = 0;
    const header = document.querySelector('header');

    window.addEventListener('scroll', function() {
        const currentScroll = window.pageYOffset;
        
        if (currentScroll <= 0) {
            header.classList.remove('scroll-up');
            return;
        }
        
        if (currentScroll > lastScroll && !header.classList.contains('scroll-down')) {
            // Scroll Down
            header.classList.remove('scroll-up');
            header.classList.add('scroll-down');
        } else if (currentScroll < lastScroll && header.classList.contains('scroll-down')) {
            // Scroll Up
            header.classList.remove('scroll-down');
            header.classList.add('scroll-up');
        }
        lastScroll = currentScroll;
    });

    // WebSocket connection for real-time updates
    const ws = new WebSocket('ws://' + window.location.host + '/ws');
    
    ws.onopen = function() {
        console.log('WebSocket connection established');
    };
    
    ws.onmessage = function(event) {
        console.log('Received message:', event.data);
        // Handle real-time updates here
    };
    
    ws.onclose = function() {
        console.log('WebSocket connection closed');
    };
    
    ws.onerror = function(error) {
        console.error('WebSocket error:', error);
    };
}); 