import React, { Component } from 'react';
import './styles/ObjectsList.css';
import ReactPaginate from 'react-paginate';

import ObjectCard from './ObjectCard.js';
import DirectoryCard from './DirectoryCard.js';

import { withRouter } from "react-router";

class ObjectsList extends Component {
    constructor(props) {
        super(props);
        this.pageSize = 10;
        this.state = {
            reload: true,
            pageNumber: 0,
            data: [],
            count: 0,
            folderPath: props.match.params.folderPath
        }
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        let { folderPath } = nextProps.match.params;
        folderPath = folderPath ? folderPath : "";
        if (folderPath !== prevState.folderPath) {
            return ({ folderPath: folderPath, reload: true });
        }
        return null
    }

    loadDataFromServer(pageNumber) {
        const limit = this.pageSize;
        const offset = this.pageSize * pageNumber;
        let { folderPath } = this.props.match.params;
        folderPath = folderPath ? folderPath : "";
        var request = new XMLHttpRequest();
        request.open('GET',
            `https://3xorekk603.execute-api.eu-central-1.amazonaws.com/default/GetS3ObjectsList?count=${limit}&offset=${offset}&folder=${folderPath}`,
            true);
        request.onload = function () {
            const response = JSON.parse(request.response);
            this.setState(
                {
                    data: response.files,
                    count: response.count,
                    reload: false,
                    folderPath: folderPath
                }
            );
        }.bind(this);
        request.send();
    }

    handlePageClick = data => {
        this.setState({ pageNumber: data.selected })
        this.loadDataFromServer(data.selected);
    };

    render() {
        if (this.state.reload) {
            this.loadDataFromServer(this.state.pageNumber);
        }
        const filesList = this.state.data.map(item => {
            if (item.directory) {
                return (
                    <li key={item.name}>
                        <DirectoryCard
                            name={item.name}
                        />
                    </li>
                );
            } else {
                return (
                    <li key={item.name}>
                        <ObjectCard
                            name={item.name}
                            size={item.size}
                            modifiedDate={item.modifiedDate}
                            downloadLink={item.downloadLink}
                            isDownloadable={item.downloadable}
                        />
                    </li>
                );
            }
        });
        return (
            <div id="content">
                <ul className="object_card_list">
                    {filesList}
                </ul>
                <div className="object_card_paginate">
                    <ReactPaginate
                        previousLabel={'previous'}
                        nextLabel={'next'}
                        breakLabel={'...'}
                        breakClassName={'break-me'}
                        pageCount={Math.ceil(this.state.count / this.pageSize)}
                        marginPagesDisplayed={2}
                        pageRangeDisplayed={5}
                        onPageChange={this.handlePageClick}
                        containerClassName={'pagination'}
                        subContainerClassName={'pages pagination'}
                        activeClassName={'active'}
                        forcePage={this.state.pageNumber}
                    />
                </div>
            </div>
        );
    }
}

export default withRouter(ObjectsList);
