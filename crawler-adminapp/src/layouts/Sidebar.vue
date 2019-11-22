<template>
    <div class="app-container app-theme-white">
        <transition name="fade" mode="out-in" appear>
            <Header />
        </transition>
        <transition name="fade" mode="out-in" appear>
            <Sidebar />
        </transition>
        <div class="app-main__outer">
            <div class="app-main__inner">
                <slot></slot>
            </div>
            <transition name="fade" mode="out-in" appear>
                <Footer/>
            </transition>
        </div>
    </div>
</template>

<script>
    import Header from "../components/Header";
    import Sidebar from "../components/Sidebar";
    import Footer from "../components/Footer";

    export default {
        name: 'app',
        components: {
            Header,
            Sidebar,
            Footer,
        },
        methods: {
        },
    };
</script>
