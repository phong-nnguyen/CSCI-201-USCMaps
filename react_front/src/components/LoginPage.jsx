import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import './LoginPage.css';

function LoginPage() {
  const [user, setUser] = useState('');
  const [pass, setPass] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const params = new URLSearchParams({
        log: 'Login',
        user,
        pass,
      });
      const res = await axios.get(`http://localhost:8080/your-app-name/loginPageBackend?${params.toString()}`);
      setMessage(res.data);
    } catch (err) {
      setMessage('Error connecting to server.');
    }
  };

  return (
    <div className="login-container">
    <img src="/image 2.png" alt="USC Logo" className="usc-logo" />
    <h2 className="welcome-title">
      Welcome to <span className="bold-title">TrojanMap</span>
    </h2>
    <form onSubmit={handleLogin} className="login-form">
      <input
        type="text"
        placeholder="Email Address"
        value={user}
        onChange={(e) => setUser(e.target.value)}
        required
      />
      <input
        type="password"
        placeholder="Password"
        value={pass}
        onChange={(e) => setPass(e.target.value)}
        required
      />
      <div className="link-row">
        <span className="link-text" onClick={() => navigate('/register')}>
          Create an account
        </span>
      </div>
      <button type="submit" className="login-btn">Log in</button>
    </form>
    <p className="error-msg">{message}</p>
  </div>
  );
}

export default LoginPage;