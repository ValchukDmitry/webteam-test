import React, { Component } from 'react';
import directory_icon from '../assets/images/folder_icon.png';

import PropTypes from 'prop-types';

import './styles/ObjectCard.css';

import { Link, withRouter } from "react-router-dom";

class DirectoryCard extends Component {
    render() {
        return (
            <Link to={`${this.props.name}/`}>
                <div className="object_card">
                    <div className="object_card_icon"><img src={directory_icon} alt="File"></img></div>
                    <p className="object_card_name">{this.props.name}</p>
                    <p className="object_card_size">--</p>
                    <p className="object_card_modified">--</p>
                </div>
            </Link>
        );
    }
}

DirectoryCard.propTypes = {
    name: PropTypes.string.isRequired,
}

export default withRouter(DirectoryCard);
