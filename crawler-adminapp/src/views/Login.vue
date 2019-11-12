<template>
    <div>
        <div class="full-height bg-plum-plate bg-animation">
            <div class="d-flex full-height justify-content-center align-items-center">
                <b-col md="10" class="mx-auto app-login-box">
                    <div class="modal-dialog w-100 mx-auto">
                        <div class="modal-content w-100">
                            <div class="modal-body p-3">
                                <div class="h5 modal-title text-center">
                                    <h4 class="mt-2">
                                        <div class="mb-3">Log In</div>
                                        <span>Please sign in to your account below.</span>
                                    </h4>
                                </div>
                                <div class="pb-3">
                                    <div class="divider"/>
                                </div>
                                <b-form-group id="exampleInputGroup1"
                                              label-for="exampleInput1">
                                    <div class="position-relative row form-group">
                                        <label class="col-sm-2 col-form-label">User ID</label>
                                        <div class="col-sm-10">
                                            <b-form-input size="lg" id="exampleInput1"
                                                v-model="username"
                                                type="text"
                                                required
                                                placeholder="Enter user ID...">
                                            </b-form-input>
                                        </div>
                                    </div>
                                </b-form-group>

                                <b-form-group id="exampleInputGroup2"
                                              label-for="exampleInput2">
                                    <div class="position-relative row form-group">
                                        <label class="col-sm-2 col-form-label">Password</label>
                                        <div class="col-sm-10">
                                            <b-form-input size="lg" id="exampleInput2"
                                                v-model="password"
                                                type="password"
                                                required
                                                placeholder="Enter password...">
                                            </b-form-input>
                                        </div>
                                    </div>
                                </b-form-group>
                            </div>
                            <div class="modal-footer clearfix">
                                <div class="mx-auto">
                                    <b-button @click="logIn" variant="primary" size="lg">Login</b-button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="text-center text-white opacity-8 mt-3">
                        Copyright &copy; ArchitectUI 2019
                    </div>
                </b-col>
            </div>
        </div>
    </div>
</template>

<script>
import AuthService from '@/services/AuthService';

export default {
    name: 'Login',
    data() {
        return {
            username : '',
            password : ''
        };
    },
    methods: {
        logIn() {
            AuthService.logIn(this.username, this.password)
            .then(data => {
                console.log('token', data.data.token);
                this.$router.push('/home');
            })
            .catch(err => {
                console.log(err);
            });
        }
     }
};
</script>

<style>

.full-height {
    height: 100vh !important;
}

</style>