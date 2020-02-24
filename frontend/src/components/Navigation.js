import React, { Component } from 'react';
import './styles/Navigation.css';

import { Link, withRouter } from "react-router-dom";

class Navigation extends Component {

    render() {
        let { pathname } = this.props.location;
        pathname = pathname ? pathname : "/";
        pathname = "/" + pathname
        const pathParts = pathname.split("/").filter(a => a)
        let path = ""
        const links = pathParts.map(pathElem => {
            path = `${path}/${pathElem}`
            return (
                  <Link key={`${path}`} to={`${path}`}>{`/${pathElem}`}</Link>
            );
        });
        links.unshift(<Link key="/" to="/">root</Link>)
        return (
            <div className="navigation">
                {links}
            </div>
        );
    }
}

export default withRouter(Navigation);
