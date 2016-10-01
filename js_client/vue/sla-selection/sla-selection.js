const axios = require("axios");
const Plotly = require("plotly.js/lib/core");

Plotly.register([
    require('plotly.js/lib/bar')
 ]);


const _ = {
    max: require("lodash/max"),
    min: require("lodash/min"),
    sum: require("lodash/sum")
};


module.exports = {
    data: function () {
        return {
            groups: [],
            latencies: []
        };
    },

    props: ['templates'],
    methods: {
        getLatencyInfo: function () {
            axios.get("/querylatency")
                .then(r => {
                    this.latencies = r.data;
                });
        }
    },

    watch: {
        templates: function(newTemplates) {
            const templateIDs = newTemplates
                      .map((itm, idx) => (itm != null) && idx)
                      .filter(itm => itm != null);

            const ourLatencies = [];
            for (let id in this.latencies) {
                let iid = parseInt(id);
                if (templateIDs.indexOf(iid) == -1)
                    continue;
                ourLatencies.push(this.latencies[id]);
            }
            
            
            const x = ["Longest query", "Shortest query", "All queries"];
            const y = [0, 0, 0];
            y[0] = _.max(ourLatencies)/1000;
            y[1] = _.min(ourLatencies)/1000;
            y[2] = _.sum(ourLatencies)/1000;

            const layout = {
                yaxis: {
                    showgrid: true,
                    gridcolor: "#bdbdbd"
                }
            };

            const y2 = [100, 100, 100];
            
            Plotly.newPlot("slaPlot",
                           [{x, y, type: "bar"},
                            {x, y: y2, type: "scatter"}],
                           layout, {displayModeBar: false});
        }
    },

    created: function () {
        this.getLatencyInfo();
    }
    
};
