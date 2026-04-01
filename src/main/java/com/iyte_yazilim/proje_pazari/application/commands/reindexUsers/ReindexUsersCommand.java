package com.iyte_yazilim.proje_pazari.application.commands.reindexUsers;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record ReindexUsersCommand() implements ICommand<ApiResponse<Void>> {}
