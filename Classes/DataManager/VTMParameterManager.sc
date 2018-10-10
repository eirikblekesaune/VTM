VTMParameterManager : VTMDataManager {
  *dataClass{ VTMParameter; }

  *new{arg itemDeclarations, parent, descriptions;
    ^super.new(itemDeclarations, parent).initParameterManager;
  }

  name{ ^\parameters; }

  initParameterManager{

  }

}
