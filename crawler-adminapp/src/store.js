import Vue from 'vue';
import Vuex from 'vuex';
import VuexPersistence from 'vuex-persist';

Vue.use(Vuex);

// Uses local storage to maintain state across sessions
const vuexLocal = new VuexPersistence({
    storage: window.localStorage,
    reducer: (state) => ({
        language : state.language,
        token : state.token
    }),
});

export default new Vuex.Store({
  state: {
    language: '',
  },
  mutations: {
    setLanguage(state, lang) {
      state.language = lang;
    },
    setToken(state, token) {
      state.token = token;
    },
    logout(state) {
      console.log('LOGOUT: SETTING TOKEN TO NULL');
      state.token = '';
    }
  },
  actions: {
    login({commit}, username, password) {

    },
    logout({commit}) {

    }
  },
  getters: {
    // Check if the user is authenticated based on the presence of access token
    isAuthenticated: state => state.token !== null && state.token !== undefined
  },
  plugins: [vuexLocal.plugin]
});