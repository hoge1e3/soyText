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

package jp.tonyu.soytext2.document;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import jp.tonyu.debug.Log;


public class IntersectIndexIterator implements IndexIterator{
    List<IndexIterator> iters=new Vector<IndexIterator>();
    IndexRecord next;
    public void add(IndexIterator it) {
        iters.add(it);
    }
    @Override
    public boolean hasNext() throws SQLException {
        //DocumentRecord[] lasts=new DocumentRecord[iters.size()];
        IndexRecord oldest=null;
        //boolean allNoNext=false;
        while (true) {
            boolean succ=true;
            for (IndexIterator it:iters) {
                if (!it.hasNext()) {
                    //Log.d("intersect", it+" has no more elements");
                    return false;
                }
                IndexRecord cur=it.next();
                //Log.d("intersect", "Get from "+it+" cur="+cur+" oldest="+oldest+" cur.lu="+cur.lastUpdate);
                if (oldest==null) {
                    oldest=cur;
                } else {
                    while (cur.lastUpdate>oldest.lastUpdate) {
                        if (!it.hasNext()) {
                            //Log.d("intersect", it+" has no more elements2");
                            return false;
                        }
                        cur=it.next();
                        //Log.d("intersect", "Get from2 "+it+" cur="+cur+" oldest="+oldest+" cur.lu="+cur.lastUpdate);
                    }
                    if (cur.lastUpdate<oldest.lastUpdate) {
                        //Log.d("intersect", "Retry: "+cur+" is older than "+oldest);
                        oldest=cur;
                        succ=false;
                        break;
                    }
                }
            }
            if (succ) {
                next=oldest;
                //Log.d("intersect", " succ next="+oldest);
                return next!=null;
            }
        }
    }

    @Override
    public IndexRecord next() throws SQLException {
        if (next==null && !hasNext()) Log.die("Next is null");
        IndexRecord res = next;
        next=null;
        return res;
    }

    @Override
    public void close() throws SQLException {
        for (IndexIterator it:iters) {
            it.close();
        }
    }

}