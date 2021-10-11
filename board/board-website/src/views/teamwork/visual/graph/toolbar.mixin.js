/*
 * @description Callbacks for toolbar
 */

export default {
    mixin ({
        graph,
        methods,
    }) {
        const $methods = {
            getGraphSize() {
                if (graph.instance) {
                    const { cacheCanvasBBox } = graph.instance.getGroup().cfg;

                    if (cacheCanvasBBox) {
                        const { width, height } = cacheCanvasBBox;

                        return [width, height];
                    }
                }
                return [0, 0];
            },

            forward() {},

            backward() {},

            relocation() {
                const width = graph.instance.getWidth() / 2 - 100;

                graph.instance.moveTo(width, 90);
            },

            zoomIn() {
                const size = methods.getGraphSize();

                graph.instance.zoom(0.9, { x: size[0] / 2, y: size[1] / 2 });
            },

            zoomOut() {
                const size = methods.getGraphSize();

                graph.instance.zoom(1.1, { x: size[0] / 2, y: size[1] / 2 });
            },

            resize() {
                const size = methods.getGraphSize();

                graph.instance.zoomTo(1, { x: size[0] / 2, y: size[1] / 2 });
            },
        };

        Object.assign(methods, $methods);

        return {
            methods,
        };
    },
};
