<template>
    <div class="app-header header-shadow">
        <div class="app-header__mobile-menu">
            <div>
                <button type="button" class="hamburger close-sidebar-btn hamburger--elastic" v-bind:class="{ 'is-active' : isOpen }" @click="toggleMobile('closed-sidebar-open')">
                    <span class="hamburger-box">
                        <span class="hamburger-inner"></span>
                    </span>
                </button>
            </div>
        </div>
        <div class="app-header__content">
            <div class="app-header-right">
                <b-button @click="logOut" class="btn-icon btn-icon-only" variant="primary" size="sm">
                    <div class="btn-icon-wrapper">
                        {{ $t('LOGOUT_BUTTON') }}
                    </div>
                </b-button>
            </div>
        </div>
    </div>
</template>

<script>
import AuthService from '@/services/AuthService';

export default {
    name: 'Header',
    data() {
        return {
            isOpen: false
        };
    },
    methods: {
        toggleMobile(className) {
            const el = document.body;
            this.isOpen = !this.isOpen;

            if (this.isOpen) {
                el.classList.add(className);
            } else {
                el.classList.remove(className);
            }
        },
        logOut() {
            AuthService.logOut()
            .then((r) => {
                console.log(r);
                this.$router.push('/');
            })
            .catch(err => {
                console.log(err);
            });
        }
    }
};
</script>
