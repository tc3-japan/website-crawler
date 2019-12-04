<template>
    <div>
        <div class="full-height bg-plum-plate  bg-animation">
            <div class="d-flex full-height justify-content-center align-items-center">
                <b-col md="10" class="mx-auto app-login-box">
                    <div class="modal-dialog w-100 mx-auto">
                        <div class="modal-content w-100">
                            <div class="modal-header login-header">
                                <div class="m-auto">
                                    <h4 class="mt-2 text-center">
                                        {{ $t('LOGIN_TITLE') }}
                                    </h4>
                                </div>
                            </div>
                            <form @submit.prevent="logIn">
                                <div class="modal-body p-3">
                                    <div class="text-center">
                                        <h4 class="mt-1">
                                            <span>{{ $t('LOGIN_SUBTITLE') }}</span>
                                        </h4>
                                    </div>
                                    <div class="pb-3">
                                        <div class="divider"/>
                                    </div>
                                        <b-alert 
                                            :show="status.message"
                                            dismissible
                                            v-model="status.visible"
                                            :variant="status.type">
                                                {{ status.message }}
                                        </b-alert>
                                    <b-form-group id="exampleInputGroup1"
                                                label-for="exampleInput1">
                                        <div class="position-relative row form-group">
                                            <label class="offset-sm-1 col-sm-3 col-form-label">{{ $t('LOGIN_USER_ID') }}</label>
                                            <div class="col-sm-7">
                                                <b-form-input size="lg" id="exampleInput1"
                                                    v-model="username"
                                                    type="text"
                                                    required
                                                    :placeholder="$t('LOGIN_USER_ID_PLACEHOLDER')">
                                                </b-form-input>
                                                <div v-if="$v.username.$error && !$v.username.required" class="error invalid-feedback">{{$t('LOGIN_VALIDATION_ID_REQUIRED')}}</div>
                                            </div>
                                        </div>
                                    </b-form-group>
                                    <b-form-group id="exampleInputGroup2"
                                                label-for="exampleInput2">
                                        <div class="position-relative row form-group">
                                            <label class="offset-sm-1 col-sm-3 col-form-label">{{ $t('LOGIN_PASSWORD') }}</label>
                                            <div class="col-sm-7">
                                                <b-form-input size="lg" id="exampleInput2"
                                                    v-model="password"
                                                    type="password"
                                                    required
                                                    :placeholder="$t('LOGIN_PASSWORD_PLACEHOLDER')">
                                                </b-form-input>
                                                <div v-if="$v.password.$error && !$v.password.required" class="error invalid-feedback">{{$t('LOGIN_VALIDATION_PASSWORD_REQUIRED')}}</div>
                                            </div>
                                        </div>
                                    </b-form-group>
                                </div>
                                <div class="modal-footer clearfix">
                                    <div class="mx-auto">
                                        <b-button type="submit" variant="primary" >{{ $t('LOGIN_BUTTON') }}</b-button>
                                    </div>
                                </div>
                            </form>
                        </div>
                    </div>
                    <div class="text-center text-white opacity-8 mt-4">
                        <div class="row justify-content-md-center mb-3">
                            <locale-changer class="col-sm-3 high-contast" />
                        </div>
                        <!-- Copyright &copy; ArchitectUI 2019 -->
                    </div>
                </b-col>
            </div>
        </div>
    </div>
</template>

<script>
import AuthService from '@/services/authService';
import LocaleChanger from '../components/LocaleChanger';
import { required } from 'vuelidate/lib/validators';

export default {
    name: 'Login',
    components : {
        LocaleChanger
    },
    data() {
        return {
            username : '',
            password : '',
            status : {},
            comments: true
        };
    },
    methods: {
        logIn() {
            this.$v.$touch();
            if (this.$v.$error)
                return;

            AuthService.logIn(this.username, this.password)
            .then(response => {
                // On success redirect to the home page
                this.$router.push('/sites');
            })
            .catch(err => {
                // An error occurred, display error message
                if (err.response.status === 401) {
                    this.status = {
                        visible : true,
                        message : this.$t('LOGIN_FAILED_INVALID'),
                        type : 'danger'
                    };
                } else {
                    this.status = {
                        visible : true,
                        message : this.$t('LOGIN_FAILED_UNKNOWN'),
                        type : 'danger'
                    };
                }
            });
        }
    },
    validations : {
        username : {
            required
        },
        password : {
            required
        }
    }
};
</script>

<style>

.full-height {
    height: 100vh !important;
}

.invalid-feedback {
  display: block;
  font-size: 1.0em;
}

</style>