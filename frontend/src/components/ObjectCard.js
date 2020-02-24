import React, { Component } from 'react';
import directory_icon from '../assets/images/folder_icon.png';
import file_icon from '../assets/images/file_icon.png';
import './styles/ObjectCard.css';

class ObjectCard extends Component {

    render() {
        const icon = this.props.isDirectory ? <img src={directory_icon} alt="Directory"></img> :
            <img src={file_icon} alt="File"></img>
        const size = this.props.isDirectory ? "--" : this.props.size;
        const modifiedDate = this.props.isDirectory ? "--" : this.props.modifiedDate;
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

export default ObjectCard;
