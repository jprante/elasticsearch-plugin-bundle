/*
 * Copyright (C) 2014 Jörg Prante
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * The interactive user interfaces in modified source and object code
 * versions of this program must display Appropriate Legal Notices,
 * as required under Section 5 of the GNU Affero General Public License.
 *
 */
package org.xbib.elasticsearch.index.analysis.decompound;

import java.util.ArrayList;
import java.util.List;

class Node {

    public String content;
    public int pos;
    private List<String> classes;
    private List<Node> children;

    public Node() {
        this.content = "";
        this.classes = new ArrayList<String>();
        this.children = new ArrayList<Node>();
    }

    public Node(String content) {
        this.content = content;
        this.classes = new ArrayList<String>();
        this.children = new ArrayList<Node>();
    }

    public Node classes(List<String> classes) {
        this.classes = classes;
        return this;
    }

    public List<String> classes() {
        return classes;
    }

    public Node children(List<Node> children) {
        this.children = children;
        return this;
    }

    public List<Node> children() {
        return children;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder()
                .append('[')
                .append(content)
                .append(',')
                .append(classes)
                .append(']');
        return sb.toString();
    }
}