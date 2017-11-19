<%@ tag description="ui presentation of the file" %>

<%@ attribute name="file" type="app.entity.StorageFileEntity" required="true" %>
<%@ attribute name="tablePrefix" type="java.lang.String" required="true" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri = "/WEB-INF/custom.tld" prefix = "u" %>

<div id="file_${file.id}" class="uploadedFile">

    <div class="uploadedFileLine">
        <div class="selectionCheckbox"><input id="${tablePrefix}-checkbox-${file.id}" type="checkbox" /></div>
        <div class="date"><u:compositeDate day="${file.eventDay}" month="${file.eventMonth}" year="${file.eventYear}" /></div>
        <div class="title">${file.title}</div>
        <div class="size"><u:humanReadable size="${file.size}" /></div>
    </div>

    <div class="uploadedFileLine">
        <div class="uploadedByLabel">uploaded by:</div>
        <div class="uploadedBy">${file.uploadedBy.displayName}</div>
        <div class="actions">
            <img alt="Download as file" src="/images/file.png" />
            <img alt="Download as ZIP-archive" src="/images/download-as-archive.png" />
        </div>
    </div>

<%--     <div>${file}</div> --%>

</div>
