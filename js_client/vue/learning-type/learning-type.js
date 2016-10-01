

module.exports = {
    data: function () {
        return {
            mode: false
        };
    },

    methods: {
        isModeSet: function () {
            return this.mode != false;
        },

        clearMode: function () {
            this.mode = false;
        }
    }
};
