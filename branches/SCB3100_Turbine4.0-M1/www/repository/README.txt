--------------------------------------------------------------------------
$Id$
--------------------------------------------------------------------------

The files in this directory are here for your convenience in building
and using Scarab. The versions included with Scarab are known to be
working with Scarab. If you decide to use another version of these
libraries, unless otherwise noted (like with servlet.jar), no support
will be provided.  Note, not all files are stored here.

--------------------------------------------------------------------------

LIBRARIES
==============
These libraries are critical to the basic Scarab functionality and are
required additionally to official Maven respositories.

Please install the versions you get from this directory (www/repository)
into your local maven repository first.

* fulcrum*.jar

  Singleton Services framework. Part of the Jakarta Turbine Project.
  Some parts of it were patched for Scarab. This is why you get it
  partially from here.
  
  http://jakarta.apache.org/turbine/

* turbine*.jar

  Turbine 3 is our webapp framework. This was patched for Scarab.

  http://jakarta.apache.org/turbine/
  
 * jndi*.jar
 
  jndi 1.2.1 can't be found in official repositories because of its license,
  but you may need this dependency as well. Download it from:
  
  http://java.sun.com/products/jndi/downloads/
 

