package com.iyte_yazilim.proje_pazari.application.queries.downloadFile;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record DownloadFileQuery(String path) implements IRequest<ApiResponse<String>> {}
