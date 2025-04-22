import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

function RegisterPage() {
  const [formData, setFormData] = useState({
    user: '',
    pass: '',
    firstName: '',
    lastName: '',
    emailAdress: '',
    phoneNumber: ''
  });

  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData((prev) => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const params = new URLSearchParams({
        log: 'Register',
        ...formData,
      });
      const res = await axios.get(`http://localhost:8080/your-app-name/loginPageBackend?${params.toString()}`);
      setMessage(res.data);
      if (res.data === 'Success') {
        setTimeout(() => navigate('/'), 2000); // redirect to login after 2 seconds
      }
    } catch (err) {
      setMessage('Error connecting to server.');
    }
  };

  return (
    <div className="login-container">
      <img src="/image 2.png" alt="USC Logo" className="usc-logo" />
      <h2 className="welcome-title">
        Create your <span className="bold-title">TrojanMap</span> account
      </h2>
      <form onSubmit={handleRegister} className="login-form">
        <input type="text" name="user" placeholder="Username" value={formData.user} onChange={handleChange} required />
        <input type="password" name="pass" placeholder="Password" value={formData.pass} onChange={handleChange} required />
        <input type="text" name="firstName" placeholder="First Name" value={formData.firstName} onChange={handleChange} required />
        <input type="text" name="lastName" placeholder="Last Name" value={formData.lastName} onChange={handleChange} required />
        <input type="email" name="emailAdress" placeholder="Email Address" value={formData.emailAdress} onChange={handleChange} required />
        <input type="tel" name="phoneNumber" placeholder="Phone Number" value={formData.phoneNumber} onChange={handleChange} required />
        <button type="submit" className="login-btn">Register</button>
      </form>
      <p className="error-msg">{message}</p>
      <p>
        Already have an account?{' '}
        <span className="link-text" onClick={() => navigate('/')}>
          Log in here
        </span>
      </p>
    </div>
  );
}

export default RegisterPage;