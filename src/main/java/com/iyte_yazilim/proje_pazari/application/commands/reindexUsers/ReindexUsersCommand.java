package com.iyte_yazilim.proje_pazari.application.commands.reindexUsers;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record ReindexUsersCommand() implements ICommand<ApiResponse<Void>> {}
