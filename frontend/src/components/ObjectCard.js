import React, { Component } from 'react';
import file_icon from '../assets/images/file_icon.png';

import PropTypes from 'prop-types';

import './styles/ObjectCard.css';

class ObjectCard extends Component {

    render() {
        const icon = <img src={file_icon} alt="File"></img>
        const size = this.props.size;
        const modifiedDate = this.props.modifiedDate;
        const downloadLink = this.props.isDownloadable &&
            <a className="object_card_download_link" href={this.props.downloadLink}>download</a>;
        return (
            <div className="object_card">
                <div className="object_card_icon">{icon}</div>
                <p className="object_card_name">{this.props.name}</p>
                <p className="object_card_size">{size}</p>
                <p className="object_card_modified">{modifiedDate}</p>
                {downloadLink}
            </div>
        );
    }
}

ObjectCard.propTypes = {
    modifiedDate: PropTypes.string.isRequired,
    downloadLink: PropTypes.string,
    isDownloadable: PropTypes.bool.isRequired,
    name: PropTypes.string.isRequired,
    size: PropTypes.number.isRequired
}

export default ObjectCard;
