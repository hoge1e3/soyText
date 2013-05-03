/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
SoyText={}; //test
SoyText.generateContent=function (d) {
	var buf="";
	var ctx={
	   d2sym:{},
	   indentC:0,
	   indentBr: function () {
		 this.indentC++;
		 return this.br();
	   },
	   dedentBr: function (){
		   this.indentC--;
		   return this.br();
	   },
	   br: function () {
		   return "\n"+this.curIndent();
	   },
	   curIndent:function () {
		   return rept("    ",this.indentC);
	   },
	   noConstructor:true
	};
	/*if (d.scope) {
		for (var k in d.scope) {
			var value=d.scope[k];
			buf+="var "+k+"="+expr(value ,ctx)+";\n";
			var id=isDocument(value);
			if (id) ctx.d2sym[id]=k;
		}
	}*/
	if (d._scope) {
        for (var k in d._scope) {
            var value=d._scope[k];
            if (isDocument(value)) {
                ctx.d2sym[value._id]=k;
            }
        }
    }
	buf+="$.extend(_,"+hash(d,ctx)+");"
	return buf;

	function expr(value,ctx) {
		if (isDocument(value)) {
	        return document(value,ctx);
		} else if (isHashBlob(value)) {
			return hashBlob(value);
	     } else if (typeof value=="number") {
	        return value;
	     } else if (typeof value=="boolean") {
	        return value+"";
	     } else if (typeof value=="function") {
	    	 return func(value,ctx);
	     } else if (typeof value=="string") {
	        return str(value,ctx);
	     } else if (value==null) {
	        return "null";
	     } else if (typeof value=="object") {
	        if (value instanceof Array) {
	           return ary(value,ctx);
	        } else {
	           return hash(value,ctx);
	        }
	     } else {
	        return "null";
	     }
	}
	function func(f,ctx) {
   	     f=SoyText.decompile(f,ctx.indentC*4);
   	     //f=f+"";
	     f=f.replace(/\r/g,"").replace(/^\n/,"").replace(/^\s*/,"").replace(/\n$/,"");
	     return f;
	}
	function hash(h,ctx) {
		var blessed;
		if (isDocument(h.constructor)) { //} || typeof(h.constructor)=="function") {  in what case?
			                             // It is comment out due to h.construcotr==Object or Array or what else
			if (!ctx.noConstructor) {
			    blessed=h.constructor;
			}
		}
        ctx.noConstructor=false;
	   var res=(blessed?"$.bless("+expr(blessed,ctx)+",":"")+"{"+ctx.indentBr();
	   var kv=[];
	   for (var key in h) {
		   if (h.hasOwnProperty && !h.hasOwnProperty(key)) continue;
		   if (( ctx.noConstructor || blessed) && key=="constructor") continue;
		   var value=h[key];
		   kv.push([key,value]);
	   }
	   kv.each(function (e,idx) {
		  var key=e[0];
		  var value=e[1];
		  var valueStr=expr(value,ctx);
		  res+=hashKey(key+"")+": "+valueStr;
		  if (idx<kv.length-1) {
			  res+=","+ctx.br();
		  }
	   });
       return res+ctx.dedentBr()+"}"+(blessed?")":"");
	}
	function isDocument(d) {
		return SoyText.isDocument(d);
	}
	function isHashBlob(d) {
		return SoyText.isHashBlob(d);
	}
    function str(s) {
	     s=s.replace(/\\/g,"\\\\")
	        .replace(/\n/g,"\\n")
	        .replace(/\r/g,"\\r")
	        .replace(/\"/g,"\\\"");
	     return "\""+s+"\"";
	}
	function ary(s,ctx) {
	    var res="["+ctx.indentBr();
	    res+=s.map(function (e) { return expr(e,ctx); }).join(", "+ctx.br());
	    res+=ctx.dedentBr()+"]";
	    return res;
	}
	function document(d,ctx) {
		 var name=ctx.d2sym[d.id];
		 if (name) {
			 return name;
		 } else {
			 return "$.byId("+str(d.id)+")";
		 }
    }
	function hashBlob(h,ctx) {
		return "$.hashBlob("+str(h.getHash())+")";
	}
    function rept(str,times) {
    	var res="";
    	for (;times>0;times--) res+=str;
    	return res;
    }
    function isSymbol(s) {
    	return s.match(/^[a-zA-Z_\$][\w\d\$]*$/);
    }
    function hashKey(s) {
    	if (isSymbol(s)) return s;
    	else return str(s);
    }
}; // tohash

