function Search_entry(entry, hits) {
  this.entry = entry;
  this.hits=hits;
};



Search_entry.prototype.hit = function() {
  this.hits++;
};


Search_entry.prototype.cooldown = function() {
  this.popularity--;
};


module.exports = Search_entry;