

module.exports = {
    data: function () {
        return {
            mode: false,
            saved: false,
            frequencies: [5,5,5,5,5,5,5,5,5,5,5]
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

        getFreqs: function() {
            return this.frequencies
                .filter((itm, idx) => this.templates.indexOf(idx) > -1);
        },

        save: function () {
            this.saved = true;
            console.log("emitting mode change");
            this.$emit("mode-changed", this.mode);
            if (this.mode == "slearn")
                this.$emit("frequency-changed",
                           this.getFreqs());
        },

        haveTemplates: function() {
            return this.templates.length != 0;
        }
    },

    watch: {
        templates: function(t) {
            this.saved = false;
            this.$emit("frequency-changed", []);
        }
    }
};
