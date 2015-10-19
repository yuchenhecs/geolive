var Trie_node = require('./trie_node.js');
var Search_entry = require('./search_entry.js');

function Trie() {
    this.root=new Trie_node();

    this.cursor=this.root;
};


Trie.prototype.insertString = function(str,value) {//key , value
  
  var curr = this.root;
  var i=0;
  for(;i<str.length;i++){
    //if(curr.direction[str.charAt(i)] == null){
    if(curr.direction[str.charAt(i)] == null){
      curr.direction[str.charAt(i)] = new Trie_node();
    }
    curr=curr.direction[str.charAt(i)];
  }

  // leaf node
  var entries_len=curr.entries.length;
  var found=false;
  var j=0
  for(;j<entries_len;j++){
    if(curr.entries[j].entry == value){
      found=true;
      break;
    }
  }

  var dest_entry;
  if(found==false){
    dest_entry=new Search_entry(value,1);
    curr.entries.push(dest_entry);
  }else{
    curr.entries[j].hit();// hit +1
    dest_entry=curr.entries[j];
  }
  
  // update topk  
  // start from root again
  curr = this.root;
  console.log("33333333333");
  i=0;
  for(;i<str.length;i++){
    var topk_len=curr.topk.length;
    var found=false;
    var j=0
    for(;j<topk_len;j++){
      if(curr.topk[j].entry == value){
        found=true;
        break;
      }
    }
    console.log("22222222222");
    if(found==true) continue;
    
    var k=3;
    if(topk_len<=k-1){
        curr.topk.push(dest_entry);  // new entry
        console.log("111111111111");
    }else{
      var tmp_min=curr.topk[0].hits;
      var tmp_index=0;
      for(var j=0;j<k;j++){
        if(curr.topk[j].hits<tmp_min){
          tmp_min=curr.topk[j].hits;
          tmp_index=j;
        }
      }

      if(tmp_min<dest_entry.hits){
        curr.topk.splice(tmp_index, 1);
        curr.topk.push(dest_entry);
      }
    }
    curr=curr.direction[str.charAt(i)];
  }
};


/*
Trie.prototype.proceedCursor = function(token) {
  if(!this.cursor) return;
  if(this.cursor.direction[token]){
    this.cursor=this.cursor.direction[token];  
  }else{
    this.cursor=null; 
  }
}

Trie.prototype.resetCursor = function() {
  this.cursor=this.root;
}
*/
Trie.prototype.getEntries = function(str) {
  var cursor=this.root;
  
  str.foreach(function(token){
    if(!cursor) return null;
    cursor=cursor.direction[token];
  });
  if(!cursor) return null;

  return cursor.entries;
}


Trie.prototype.getTopk = function(str) {
  var cursor=this.root;
  
  str.foreach(function(token){
    if(!cursor) return null;
    cursor=cursor.direction[token];
  });
  if(!cursor) return null;

  return cursor.topk;
}

//TODO: delete node!!





module.exports = Trie;