// Global variables
let map;
let directionsService;
let directionsRenderer;
let originAutocomplete;
let destinationAutocomplete;
let userMarker;

// Initialize map when Google Maps API is loaded
function initMap() {
    // USC coordinates (centered on Tommy Trojan)
    const uscCenter = { lat: 34.0205, lng: -118.2856 };
    
    // Create a new map centered on USC
    map = new google.maps.Map(document.getElementById("map"), {
        center: uscCenter,
        zoom: 16,
        mapTypeControl: true,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
            position: google.maps.ControlPosition.TOP_RIGHT
        },
        zoomControl: true,
        zoomControlOptions: {
            position: google.maps.ControlPosition.RIGHT_CENTER,
        },
        streetViewControl: true,
        streetViewControlOptions: {
            position: google.maps.ControlPosition.RIGHT_BOTTOM,
        },
        fullscreenControl: true,
        fullscreenControlOptions: {
            position: google.maps.ControlPosition.RIGHT_TOP,
        }
    });
    
    // Initialize directions service and renderer
    directionsService = new google.maps.DirectionsService();
    directionsRenderer = new google.maps.DirectionsRenderer({
        map: map,
        panel: document.getElementById("directions-panel")
    });
    
    // Initialize autocomplete for input fields
    originAutocomplete = new google.maps.places.Autocomplete(
        document.getElementById("origin"),
        { componentRestrictions: { country: "us" } }
    );
    
    destinationAutocomplete = new google.maps.places.Autocomplete(
        document.getElementById("destination"),
        { componentRestrictions: { country: "us" } }
    );
    
    // Set bounds for autocomplete to prioritize USC area
    const uscBounds = new google.maps.LatLngBounds(
        new google.maps.LatLng(34.0152, -118.2912), // Southwest
        new google.maps.LatLng(34.0272, -118.2793)  // Northeast
    );
    
    originAutocomplete.setBounds(uscBounds);
    destinationAutocomplete.setBounds(uscBounds);
    
    // Get user's current location if permissions are granted
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            (position) => {
                const userLocation = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };
                
                // Create user location marker
                userMarker = new google.maps.Marker({
                    position: userLocation,
                    map: map,
                    icon: {
                        path: google.maps.SymbolPath.CIRCLE,
                        fillColor: "#4285F4",
                        fillOpacity: 1,
                        strokeColor: "white",
                        strokeWeight: 2,
                        scale: 7
                    },
                    title: "Your Location"
                });
                
                // If user is close to USC, center the map on their location
                const uscLatLng = new google.maps.LatLng(uscCenter.lat, uscCenter.lng);
                const userLatLng = new google.maps.LatLng(userLocation.lat, userLocation.lng);
                const distance = google.maps.geometry.spherical.computeDistanceBetween(uscLatLng, userLatLng);
                
                // If within 5km of USC, center on user
                if (distance < 5000) {
                    map.setCenter(userLocation);
                }
                
                // Add event listeners for location buttons
                setupLocationButtons(userLocation);
            },
            () => {
                // Error handler - permissions denied
                console.log("User denied geolocation permission");
                setupLocationButtons(null);
            }
        );
    } else {
        // Browser doesn't support geolocation
        console.log("Browser doesn't support geolocation");
        setupLocationButtons(null);
    }
    
    // Add event listener for calculate route button
    document.getElementById("calculate-route").addEventListener("click", calculateRoute);
    
    // Setup dropdown functionality
    setupDropdowns();
    
    // Update user name from local storage
    updateUserName();
}

// Setup user location buttons
function setupLocationButtons(userLocation) {
    const originLocationButton = document.getElementById("origin-location");
    const destLocationButton = document.getElementById("destination-location");
    
    originLocationButton.addEventListener("click", () => {
        if (userLocation) {
            // Reverse geocode user's location to get address
            const geocoder = new google.maps.Geocoder();
            geocoder.geocode({ location: userLocation }, (results, status) => {
                if (status === "OK" && results[0]) {
                    document.getElementById("origin").value = results[0].formatted_address;
                } else {
                    console.error("Geocoder failed due to: " + status);
                    document.getElementById("origin").value = `${userLocation.lat}, ${userLocation.lng}`;
                }
            });
        } else {
            alert("Your location is not available");
        }
    });
    
    destLocationButton.addEventListener("click", () => {
        if (userLocation) {
            // Reverse geocode user's location to get address
            const geocoder = new google.maps.Geocoder();
            geocoder.geocode({ location: userLocation }, (results, status) => {
                if (status === "OK" && results[0]) {
                    document.getElementById("destination").value = results[0].formatted_address;
                } else {
                    console.error("Geocoder failed due to: " + status);
                    document.getElementById("destination").value = `${userLocation.lat}, ${userLocation.lng}`;
                }
            });
        } else {
            alert("Your location is not available");
        }
    });
}

// Calculate and display a route
function calculateRoute() {
    const origin = document.getElementById("origin").value;
    const destination = document.getElementById("destination").value;
    
    if (!origin || !destination) {
        alert("Please enter both origin and destination");
        return;
    }
    
    const request = {
        origin: origin,
        destination: destination,
        travelMode: google.maps.TravelMode.WALKING, // Default to walking mode for campus
        unitSystem: google.maps.UnitSystem.IMPERIAL,
        provideRouteAlternatives: true
    };
    
    directionsService.route(request, (result, status) => {
        if (status === "OK") {
            directionsRenderer.setDirections(result);
            
            // Show route info
            displayRouteInfo(result);
            
            // Show directions panel
            document.getElementById("directions-panel").classList.add("active");
        } else {
            alert("Directions request failed due to " + status);
        }
    });
}

// Display route information
function displayRouteInfo(result) {
    const route = result.routes[0];
    const leg = route.legs[0];
    
    // Calculate ETA
    const now = new Date();
    const arrivalTime = new Date(now.getTime() + leg.duration.value * 1000);
    const arrivalTimeStr = arrivalTime.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' });
    
    // Create route info HTML
    const routeInfoElement = document.getElementById("route-info");
    routeInfoElement.innerHTML = `
        <div class="route-summary">
            <div class="route-distance">${leg.distance.text}</div>
            <div class="route-duration">${leg.duration.text}</div>
        </div>
        <div class="route-eta">Estimated arrival: ${arrivalTimeStr}</div>
        <div class="route-addresses">
            <div class="route-origin"><strong>From:</strong> ${leg.start_address}</div>
            <div class="route-destination"><strong>To:</strong> ${leg.end_address}</div>
        </div>
    `;
    
    // Show route info panel
    routeInfoElement.classList.add("active");
}

// Setup location dropdown functionality
function setupDropdowns() {
    const originInput = document.getElementById("origin");
    const destinationInput = document.getElementById("destination");
    const originDropdown = document.getElementById("origin-dropdown");
    const destinationDropdown = document.getElementById("destination-dropdown");
    const originArrow = originInput.nextElementSibling.nextElementSibling;
    const destinationArrow = destinationInput.nextElementSibling.nextElementSibling;
    
    // Toggle origin dropdown
    originArrow.addEventListener("click", () => {
        originDropdown.classList.toggle("active");
        destinationDropdown.classList.remove("active");
    });
    
    // Toggle destination dropdown
    destinationArrow.addEventListener("click", () => {
        destinationDropdown.classList.toggle("active");
        originDropdown.classList.remove("active");
    });
    
    // Handle origin dropdown item selection
    document.querySelectorAll("#origin-dropdown .dropdown-item").forEach(item => {
        item.addEventListener("click", () => {
            originInput.value = item.getAttribute("data-value");
            originDropdown.classList.remove("active");
        });
    });
    
    // Handle destination dropdown item selection
    document.querySelectorAll("#destination-dropdown .dropdown-item").forEach(item => {
        item.addEventListener("click", () => {
            destinationInput.value = item.getAttribute("data-value");
            destinationDropdown.classList.remove("active");
        });
    });
    
    // Close dropdowns when clicking outside
    document.addEventListener("click", (e) => {
        if (!e.target.closest(".location-input-group")) {
            originDropdown.classList.remove("active");
            destinationDropdown.classList.remove("active");
        }
    });
}

// Update user name from localStorage
function updateUserName() {
    const loggedInUser = localStorage.getItem("loggedInUser");
    const userNameElement = document.getElementById("user-name");
    
    if (loggedInUser) {
        userNameElement.textContent = loggedInUser;
        
        // Also update username in header if it exists
        const headerUsername = document.getElementById("username");
        if (headerUsername) {
            headerUsername.textContent = loggedInUser;
            // Show welcome message
            document.querySelector(".user-welcome").style.display = "inline-block";
            document.querySelector(".logout-link").style.display = "inline-block";
            // Hide login/register links
            document.querySelectorAll(".auth-links").forEach(link => {
                link.style.display = "none";
            });
        }
    } else {
        userNameElement.textContent = "Trojan";
    }
    
    // Setup logout button
    const logoutBtn = document.getElementById("logout-btn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", function(e) {
            e.preventDefault();
            localStorage.removeItem("loggedInUser");
            window.location.reload();
        });
    }
} 