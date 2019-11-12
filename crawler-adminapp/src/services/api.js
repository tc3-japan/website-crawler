import axios from 'axios';
import config from '../../config';

export default() => {

    return axios.create({
        baseURL: config.api.baseURL,
        withCredentials: true,
        useCredentails: true,
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*'
        }
    });
};