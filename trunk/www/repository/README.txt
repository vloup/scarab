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

Please install the versions of this special jars into your local repository, 
except the ones from Scarab's local repository, which will be taken directly from there.

* fulcrum*.jar (Scarab's repository)

  Singleton Services framework. Part of the Jakarta Turbine Project.
  Some parts of it were patched for Scarab. This is why you get it
  partially from here.
  
  http://jakarta.apache.org/turbine/

* turbine*.jar (Scarab's repository)

  Turbine 3 is our webapp framework. This was patched for Scarab.

  http://jakarta.apache.org/turbine/
  
 * jndi*.jar
 
  jndi 1.2.1 can't be found in official repositories because of its license,
  but you may need this dependency as well. Download it from:
  
  http://java.sun.com/products/jndi/downloads/


ARTIFACT MANAGER
==============

Install any artifact manager like Artifactory or Nexus. 
This might ease the task of development. See:

-> http://www.jfrog.com/home/v_artifactory_opensource_overview
-> http://www.sonatype.org/nexus/

Then you may want to include

      <repositories>
        <repository>
          <id>maven2-repository.dev.java.net</id>
          <name>Java.net Repository for Maven</name>
          <url>http://download.java.net/maven/2/</url>
          <layout>default</layout>
        </repository>
      </repositories>
	  
into your maven repository, to support some of the Sun's special jars, e.g.: jdbc-stdext.

You might have to upload all jars from the local www/repository, too.

