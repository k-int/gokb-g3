<dl class="dl-horizontal">

  <dt><g:annotatedLabel owner="${d}" property="name">User Organisation Name</g:annotatedLabel></dt>
  <dd><g:xEditable class="ipe" owner="${d}" field="displayName" /></dd>

  <g:if test="${d.id != null}">

  <dt><g:annotatedLabel owner="${d}" property="name">Members</g:annotatedLabel></dt>
  <dd>
    <table class="table table-bordered table-striped">
      <thead>
        <tr>
          <td>User</td>
          <td>Membershop Status</td>
          <td>Role</td>
          <td>Actions</td>
        </tr>
      </thead>
      <tbody>
        <g:each in="${d.members}" var="m">
          <tr>
            <td>${m.party.displayName}</td>
            <td>${m.status?.value}</td>
            <td>${m.role?.value}</td>
            <td></td>
          </tr>
        </g:each>
      </tbody>
      <tfoot>

        <g:form controller="ajaxSupport" action="addToCollection" class="form-inline">
          <input type="hidden" name="__context" value="org.gokb.cred.UserOrganisation:${d.id}"/>
          <input type="hidden" name="__recip" value="memberOf"/>
          <input type="hidden" name="__newObjectClass" value="org.gokb.cred.UserOrganisationMembership"/>
          <tr>
            <td>
              <g:simpleReferenceTypedown class="form-control" 
                                         name="party" 
                                         baseClass="org.gokb.cred.User"/>
            </td>
            <td>
              <g:simpleReferenceTypedown class="form-control" 
                                         name="status" 
                                         baseClass="org.gokb.cred.RefdataValue" 
                                         filter1="MembershipStatus"/>
            </td>
            <td>
              <g:simpleReferenceTypedown class="form-control" 
                                         name="role" 
                                         baseClass="org.gokb.cred.RefdataValue" 
                                         filter1="MembershipRole"/>
            </td>
            <td><button class="btn btn-success">Add</button></td>
          </tr>
        </g:form>
      </tfoot>
    </table>
  </dd>


  <dt><g:annotatedLabel owner="${d}" property="name">Folders / Collections</g:annotatedLabel></dt>
  <dd>
    <table class="table table-bordered table-striped">
      <thead>
        <tr>
          <td>Name</td>
          <td>Actions</td>
        </tr>
      </thead>
      <tbody>
        <g:each in="${d.folders}" var="f">
          <tr>
            <td><g:link controller="resource" action="show" id="org.gokb.cred.Folder:${f.id}">${f.name}</g:link></td>
            <td></td>
          </tr>
        </g:each>
      </tbody>
      <tfoot>
        <g:form controller="ajaxSupport" action="addToCollection" class="form-inline">
          <input type="hidden" name="__context" value="org.gokb.cred.UserOrganisation:${d.id}"/>
          <input type="hidden" name="__recip" value="owner"/>
          <input type="hidden" name="__newObjectClass" value="org.gokb.cred.Folder"/>
          <tr>
            <td>
              <input type="text" class="form-control" name="name" placeholder="New Folder Name"/>
            </td>
            <td><button class="btn btn-success">Add</button></td>
          </tr>
        </g:form>
      </tfoot>
    </table>
  </dd>

  <dt><g:annotatedLabel owner="${d}" property="name">Load Title List</g:annotatedLabel> (<a href="https://github.com/k-int/gokb-phase1/wiki/Title-List-Upload-Format">Format</a>)</dt>
  <dd class="container">
    <g:form controller="folderUpload" action="processSubmission" method="post" enctype="multipart/form-data">
      <input type="hidden" name="ownerOrg" value="${d.id}"/>
      <div class="input-group" >
        <span class="input-group-btn">
          <span class="btn btn-default btn-file">
            Browse <input type="file" id="submissionFile" name="submissionFile" onchange='$("#upload-file-info").html($(this).val());' />
          </span>
        </span>
        <span class='form-control' id="upload-file-info"><label for="submissionFile" >Select a file...</label></span>
        <span class="input-group-btn">
          <button type="submit" class="btn btn-primary">Upload</button>
        </span>
      </div>
    </g:form>
  </dd>
  </g:if>

</dl>
