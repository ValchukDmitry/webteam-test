import React, { Component } from "react";
import {
  BrowserRouter as Router,
  Switch,
  Route
} from "react-router-dom";

import ObjectsList from './components/ObjectsList'
import Navigation from './components/Navigation'

class App extends Component {
  render() {
    return (
      <Router>
        <Navigation />
        <Switch>
          <Route path="/:folderPath+">
            <ObjectsList />
          </Route>
          <Route path="/" exact>
            <ObjectsList />
          </Route>
        </Switch>
      </Router >
    );
  }
}

export default App;