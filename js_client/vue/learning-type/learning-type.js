

module.exports = {
    data: function () {
        return {
            mode: false,
            saved: false,
            frequencies: [5,5,5,5,5,5,5,5,5,5,5]
        };
    },

    props: ["templates", "deadline"],

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
            this.$emit("mode-changed", this.mode);
           if (this.deadline && this.mode) {
               $("html, body").animate({ scrollTop: $("#results").offset().top }, 900);
           }
        },

        haveTemplates: function() {
            return this.templates.length != 0;
        }
    },

    watch: {
        templates: function(t) {
            this.clear();
        }
    }
};
