import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import keycloak from './keycloak';

const root = ReactDOM.createRoot(document.getElementById('root'));

// Initialize Keycloak here FIRST
keycloak.init({ 
  onLoad: 'login-required',
  checkLoginIframe: false,
  pkceMethod: 'S256' // Standard secure handshake
}).then((authenticated) => {
  if (authenticated) {
    // We pass the authenticated 'keycloak' instance directly to App!
    // StrictMode is removed here to stop double-render glitches
    root.render(
      <App keycloak={keycloak} />
    );
  } else {
    console.warn("User not authenticated, forcing redirect...");
    keycloak.login(); // Explicitly force login instead of an infinite reload
  }
}).catch((error) => {
  console.error("Keycloak initialization failed", error);
  root.render(
    <div style={{ padding: '40px', fontFamily: 'sans-serif', textAlign: 'center' }}>
      <h1 style={{ color: '#e74c3c' }}>Authentication Error</h1>
      <p>Could not connect to the security server.</p>
      <p style={{ color: '#7f8c8d' }}>Please check if Keycloak is running on port 8081 and that the client 'school-ui' exists in your 'school-realm'.</p>
    </div>
  );
});