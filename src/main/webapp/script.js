// Google Maps initialization
//hello
let map;
let directionsService;
let directionsRenderer;
let placesService;
let originMarker;
let destinationMarker;
let originAutocomplete;
let destinationAutocomplete;

// Initialize the map
function initMap() {
    // Create map centered at USC
    const uscCenter = { lat: 34.0224, lng: -118.2851 };
    
    // Check login status and display welcome message
    const loggedInUser = localStorage.getItem('loggedInUser');
    if (loggedInUser) {
        const welcomeMessage = document.getElementById('welcome-message');
        const welcomeUsername = document.getElementById('welcome-username');
        if (welcomeMessage && welcomeUsername) {
            welcomeMessage.style.display = 'block';
            welcomeUsername.textContent = loggedInUser;
        }
    }
    
    map = new google.maps.Map(document.getElementById("map"), {
        center: uscCenter,
        zoom: 15,
        mapTypeControl: true,
        mapTypeControlOptions: {
            style: google.maps.MapTypeControlStyle.DROPDOWN_MENU,
            position: google.maps.ControlPosition.TOP_RIGHT
        },
        streetViewControl: true,
        fullscreenControl: true,
        zoomControl: true
    });

    // Initialize services
    directionsService = new google.maps.DirectionsService();
    directionsRenderer = new google.maps.DirectionsRenderer({
        map: map,
        panel: document.getElementById("directions-panel")
    });
    placesService = new google.maps.places.PlacesService(map);

    // Create autocomplete for origin and destination inputs
    originAutocomplete = new google.maps.places.Autocomplete(
        document.getElementById("origin"),
        { fields: ["formatted_address", "geometry", "name"] }
    );

    destinationAutocomplete = new google.maps.places.Autocomplete(
        document.getElementById("destination"),
        { fields: ["formatted_address", "geometry", "name"] }
    );
	
	const loc = new google.maps.marker.AdvancedMarkerElement({
	      map,
	      position: uscCenter,
	      title: "USC",
	    });
	console.log(loc);

    const uscBounds = new google.maps.LatLngBounds(
        new google.maps.LatLng(34.015, -118.295),
        new google.maps.LatLng(34.028, -118.275)
    );
    originAutocomplete.setBounds(uscBounds);
    destinationAutocomplete.setBounds(uscBounds);

    // Add event listeners
    document.getElementById("calculate-route").addEventListener("click", calculateRoute);
    
    // Set up location selection dropdowns
    
    // Set up the user's name
    if (loggedInUser) {
        document.getElementById('user-name').textContent = loggedInUser;
    }
}

// Calculate route between origin and destination
function calculateRoute() {
    const origin = document.getElementById("origin").value;
    const destination = document.getElementById("destination").value;
    
    if (!origin || !destination) {
        alert("Please enter both origin and destination");
        return;
    }
    
    directionsService.route(
        {
            origin: origin,
            destination: destination,
            travelMode: google.maps.TravelMode.WALKING
        },
        (response, status) => {
            if (status === "OK") {
                directionsRenderer.setDirections(response);
                showRouteInfo(response);
                
                // Show the directions panel
                const directionsPanel = document.getElementById("directions-panel");
                const routeInfo = document.getElementById("route-info");
                
                if (directionsPanel) {
                    directionsPanel.classList.add('active');
                    directionsPanel.style.display = 'block';
                }
                
                if (routeInfo) {
                    routeInfo.classList.add('active');
                    routeInfo.style.display = 'block';
                }
                
                console.log("Directions should be visible now");
            } else {
                alert("Directions request failed due to " + status);
            }
        }
    );
}

// Show route information
function showRouteInfo(response) {
    const route = response.routes[0];
    const routeInfoDiv = document.getElementById("route-info");
    
    let totalDistance = 0;
    let totalDuration = 0;
    
    // Calculate total distance and duration
    route.legs.forEach(leg => {
        totalDistance += leg.distance.value;
        totalDuration += leg.duration.value;
    });
    
    // Convert to readable format
    const distanceText = totalDistance < 1000 
        ? totalDistance + " m" 
        : (totalDistance / 1000).toFixed(1) + " km";
    
    const minutes = Math.round(totalDuration / 60);
    const durationText = minutes <= 1 
        ? "1 minute" 
        : minutes + " minutes";
    
    // Display info
    routeInfoDiv.innerHTML = `
        <div class="route-stat">
            <i class="fas fa-walking"></i>
            <span>${distanceText}</span>
        </div>
        <div class="route-stat">
            <i class="fas fa-clock"></i>
            <span>${durationText}</span>
        </div>
    `;
}

// Setup location dropdowns

// Get current location
//document.getElementById('origin-location').addEventListener('click', function() {
    //getCurrentLocation(document.getElementById('origin'));
//});

const origin = document.getElementById('origin-location');
if(origin){
	origin.addEventListener('click', function() {
	    getCurrentLocation(document.getElementById('origin'));
	});
}

//document.getElementById('destination-location').addEventListener('click', function() {
 //   getCurrentLocation(document.getElementById('destination'));
//});

const destination = document.getElementById('destination-location');
if(destination){
	destination.addEventListener('click', function() {
	    getCurrentLocation(document.getElementById('destination'));
	});
}

function getCurrentLocation(inputElement) {
    if (navigator.geolocation) {
        // navigator.geolocation.getCurrentPosition(
			navigator.geolocation.watchPosition(
            (position) => {

				const {latitude,longitude} = position.coords;
				console.log("Geolocation success:", latitude, longitude);

				var jsonData = {
				        		"email" : localStorage.getItem("loggedInUser"),
				          "latitude" : latitude,
				          "longitude" : longitude,
				        }

		        fetch("reportLocation", {method: "POST", headers:{"Content-Type":"application/json"}, body: JSON.stringify(jsonData)})
				.catch(err => console.error('error', err));
					  
                const geocoder = new google.maps.Geocoder();
                const latlng = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };
                
                geocoder.geocode({ location: latlng }, (results, status) => {
                    if (status === "OK" && results[0]) {
                        inputElement.value = results[0].formatted_address;
                    } else {
                        alert("Geocoder failed due to: " + status);
                    }
                });
            },
            () => {
                alert("Error: The Geolocation service failed.");
            }
        );
    } else {
        alert("Error: Your browser doesn't support geolocation.");
    }
} 