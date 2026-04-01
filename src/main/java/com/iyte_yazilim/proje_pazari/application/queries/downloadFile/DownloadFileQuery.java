package com.iyte_yazilim.proje_pazari.application.queries.downloadFile;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record DownloadFileQuery(String path) implements IRequest<ApiResponse<String>> {}
