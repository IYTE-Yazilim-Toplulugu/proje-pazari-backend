package com.iyte_yazilim.proje_pazari.application.queries.listBannedIps;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.BannedIpDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record ListBannedIpsQuery() implements IRequest<ApiResponse<List<BannedIpDTO>>> {}
