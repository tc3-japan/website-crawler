import Api from './api';

function logIn(username, password) {
    return Api.get('/oauth/token', { username, password });
};

function logOut() {
    return Api.get('/oauth/logout');
};

export default {
    logIn,
    logOut
}