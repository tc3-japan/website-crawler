import Api from './api';

function logIn(username, password) {
    return Api().post('/oauth/token', { username, password });
}

function logOut() {
    return Api().get('/oauth/logout');
}

export default {
    logIn,
    logOut
};