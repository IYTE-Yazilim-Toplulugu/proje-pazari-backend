package com.iyte_yazilim.proje_pazari.application.queries.getFlaggedContent;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.FlaggedContentDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;

public record GetFlaggedContentQuery(int page, int size, String status, String contentType)
        implements IRequest<ApiResponse<PagedResponse<FlaggedContentDTO>>> {}
