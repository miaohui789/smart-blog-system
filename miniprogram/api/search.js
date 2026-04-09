const request = require('../utils/request')

function searchAll(params) {
  return request.get('/search', params)
}

function getHotSearchBoard(params) {
  return request.get('/search/hot', params)
}

module.exports = {
  searchAll,
  getHotSearchBoard
}
