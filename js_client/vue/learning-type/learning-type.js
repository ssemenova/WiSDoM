

module.exports = {
    data: function () {
        return {
            mode: false,
            saved: false,
            frequencies: [10,10,10,10,10,10,10,10,10,10,10]
        };
    },

    props: ["templates"],
    
    computed: {
        isSLearn: function () {
            return this.mode == "slearn";
        }
    },

    methods: {
        clear: function () {
            this.mode = false;
            this.saved = false;
            this.$emit("mode-changed", false);
        },

        save: function () {
            this.saved = true;
            this.$emit("mode-changed", this.mode); // slearn
            this.$emit("frequency-changed", this.frequencies);
        },

        haveTemplates: function() {
            return this.templates.length != 0;
        }
    },

    watch: {
        mode: function(m) {
            if (this.mode == "rlearn") {
                this.$emit("mode-changed", m);
                this.saved = true;
            }
        }
    }
};
