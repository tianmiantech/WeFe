<template>
    <p class="pw-strength">
        <span class="f12">密码强度</span>
        <i class="strength-level ml10 level-1"></i>
        <i v-if="pwStrength >= 2" class="strength-level level-2"></i>
        <i v-if="pwStrength >= 3" class="strength-level level-3"></i>
        <span v-if="pwStrength <= 2" class="f12">较弱</span>
        <span v-if="pwStrength >= 3" class="f12">较强</span>
    </p>
</template>

<script>
    export default {
        props: {
            password: String,
        },
        computed: {
            pwStrength: (vm) => {
                const { password } = vm;

                let count = 0;

                if(password.length >= 8) {
                    if(/\d/.test(password)) {
                        count++;
                    }
                    if(/[a-z|A-z]/.test(password)) {
                        count++;
                    }
                    if(/\W/.test(password)) {
                        count++;
                    }
                }

                return count;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .pw-strength{
        .strength-level{
            width: 30px;
            height: 6px;
            margin-right:4px;
            display: inline-block;
            vertical-align:middle;
        }
        .level-1{background:#D54724;}
        .level-2{background:#E98737;}
        .level-3{background:#76A030;}
    }
</style>
