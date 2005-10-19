/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package org.antlr.works.util;

import javax.swing.*;

public class IconManager {

    // @todo replace this with run-time location ?
    public static final String path = "org/antlr/works/icons/";

    protected static IconManager shared = null;

    public static IconManager shared() {
        if(shared == null)
            shared = new IconManager();
        return shared;
    }

    public ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = this.getClass().getClassLoader().getResource(path);
        return imgURL != null ? new ImageIcon(imgURL) : null;
    }

    public ImageIcon getIconApplication() {
        return createImageIcon(path+"app.png");
    }

    public ImageIcon getIconHiddenAction() {
        return createImageIcon(path+"hidden_action.png");
    }

    public ImageIcon getIconWarning() {
        return createImageIcon(path+"warning.png");
    }

    public ImageIcon getIconBackward() {
        return createImageIcon(path+"back.png");
    }

    public ImageIcon getIconForward() {
        return createImageIcon(path+"forward.png");
    }

    public ImageIcon getIconShowLinks() {
        return createImageIcon(path+"show_links.png");
    }

    public ImageIcon getIconRun() {
        return createImageIcon(path+"run.png");
    }

    public ImageIcon getIconExpandAll() {
        return createImageIcon(path+"expandall.png");
    }

    public ImageIcon getIconCollapseAll() {
        return createImageIcon(path+"collapseall.png");
    }

    public ImageIcon getIconCollapseDown() {
        return createImageIcon(path+"collapsedown.png");
    }

    public ImageIcon getIconCollapseUp() {
        return createImageIcon(path+"collapseup.png");
    }

    public ImageIcon getIconExpand() {
        return createImageIcon(path+"expand.png");
    }

    public ImageIcon getIconStop() {
        return createImageIcon(path+"stop.png");
    }

    public ImageIcon getIconStepForward() {
        return createImageIcon(path+"stepforward.png");
    }

    public ImageIcon getIconStepBackward() {
        return createImageIcon(path+"stepbackward.png");
    }

    public ImageIcon getIconGoToStart() {
        return createImageIcon(path+"gotostart.png");
    }

    public ImageIcon getIconGoToEnd() {
        return createImageIcon(path+"gotoend.png");
    }

    public ImageIcon getIconTokens() {
        return createImageIcon(path+"tokens.png");
    }

    public ImageIcon getIconParser() {
        return createImageIcon(path+"parser.png");
    }

    public ImageIcon getIconLexer() {
        return createImageIcon(path+"lexer.png");
    }

}
