/** Oracle JET Webpack aliases. API requests call the gateway directly. */
const path = require('path');

module.exports = {
  webpack: ({ context, config }) => {
    // JET's RequireJS-style absolute module IDs need explicit Webpack aliases.
    config.resolve = config.resolve || {};
    config.resolve.alias = Object.assign({}, config.resolve.alias, {
      config: path.resolve(__dirname, 'src/js/config'),
      models: path.resolve(__dirname, 'src/js/models'),
      services: path.resolve(__dirname, 'src/js/services'),
      viewModels: path.resolve(__dirname, 'src/js/viewModels')
    });

    return { context, webpack: config };
  }
};
