function url(s) {
    const l = window.location;
    return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + s;
}


document.addEventListener("DOMContentLoaded", function () {
    let main = new Vue({
        el: '#data',
        data: {
            vms: {}
        },

        methods: {
            provisionVM: function (vmID, vmType) {
                Vue.set(this.vms, vmID, {"id": vmID,
                                         "state": "starting",
                                         "queue": [],
                                         "type": vmType});
            },

            markVMReady: function (vmID) {
                this.vms[vmID].state = "running";
            },

            shutdownVM: function (vmID) {
                this.vms[vmID].state = "off";
            },

            assignQuery: function (vmID, queryID) {
                this.vms[vmID].queue.push(queryID);
            },

            queryComplete: function (queryID) {
                // TODO this scans all VMs looking for the query. we could
                // keep a mapping.
                for (let k in this.vms) {
                    this.vms[k].queue = this.vms[k].queue.filter(q => q != queryID);
                }
            }
        }
    });


    const socket = new WebSocket(url("/bandit"));

    socket.onmessage = function (event) {
        let data = event.data;
        data = JSON.parse(data);
        const msgType = data["type"];
        switch (msgType) {
        case "assign":
            main.assignQuery(data.vmID, data.queryID);
            break;
        case "complete":
            main.queryComplete(data.queryID);
            break;
        case "provision":
            main.provisionVM(data.vmID, data.vmType);
            break;
        case "ready":
            main.markVMReady(data.vmID);
            break;
        case "shutdown":
            main.shutdownVM(data.vmID);
            break;
        case "cost":
            console.log(data.cost + " at " + data.tick);
            break;
        }
    };
});
