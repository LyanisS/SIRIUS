import React from 'react';
import {Link} from "react-router-dom";

export default function Navbar(){
    return (
            <ul className="nav justify-content-center my-3">
                <li className="nav-item">
                    <Link className="nav-link" to="/">Home</Link>
                </li>
                <li className="nav-item">
                    <Link className="nav-link" to="/sample">Sample</Link>
                </li>
            </ul>
    );
};