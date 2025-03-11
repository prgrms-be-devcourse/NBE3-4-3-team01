"use client";

import { useEffect } from "react";
import { getRoleFromCookie, RoleData } from "@/lib/utils/CookieUtil";

interface ChannelFunction extends Function {
  c: (args: any) => void;
  q: any[];
}

declare global {
  interface Window {
    ChannelIO?: any;
    ChannelIOInitialized?: boolean;
  }
}

export default function ChannelTalk() {
  useEffect(() => {
    
    const loadChannelTalk = () => {
      
      const roleData: RoleData | null = getRoleFromCookie();
      const userType = roleData?.role || "ANONYMOUS";
      
      (function() {
        var w = window;
        if (w.ChannelIO) {
          return;
        }
        var ch: any = function() {
          ch.c(arguments);
        };
        ch.q = [];
        ch.c = function(args: any) {
          ch.q.push(args);
        };
        w.ChannelIO = ch;
        
        function l() {
          if (w.ChannelIOInitialized) {
            return;
          }
          w.ChannelIOInitialized = true;
          var s = document.createElement('script');
          s.type = 'text/javascript';
          s.async = true;
          s.src = 'https://cdn.channel.io/plugin/ch-plugin-web.js';
          s.charset = 'UTF-8';
          var x = document.getElementsByTagName('script')[0];
          if (x && x.parentNode) {
            x.parentNode.insertBefore(s, x);
          } else {
            document.head.appendChild(s);
          }
        }
        
        if (document.readyState === 'complete') {
          l();
        } else {
          w.addEventListener('DOMContentLoaded', l, false);
          w.addEventListener('load', l, false);
        }
      })();
      
    
      window.ChannelIO('boot', {
        "pluginKey": "261b6e67-9783-4fca-84c3-c52dde819853",
        "profile": {
          "role": userType
        },
        "hideChannelButtonOnBoot": false,
      });
      
    };

    loadChannelTalk();

    return () => {
      if (window.ChannelIO) {
        window.ChannelIO('shutdown');
        window.ChannelIOInitialized = false;
      }
    };
  }, []);

  return null;
} 