package org.zenframework.z8.pde.refactoring.messages;

import org.eclipse.osgi.util.NLS;

public final class RefactoringMessages extends NLS {

	private static final String BUNDLE_NAME = "org.zenframework.z8.pde.refactoring.messages.refactoring";

	private RefactoringMessages() {
	}

	public static String status_cannotUseDeviceOnPath;
	public static String status_coreException;
	public static String status_evaluationError;
	public static String status_IOException;
	public static String status_indexOutOfBounds;
	public static String status_invalidContents;
	public static String status_invalidDestination;
	public static String status_invalidName;
	public static String status_invalidFolder;
	public static String status_invalidPath;
	public static String status_invalidProject;
	public static String status_invalidResource;
	public static String status_invalidResourceType;
	public static String status_invalidSibling;
	public static String status_nameCollision;
	public static String status_noLocalContents;
	public static String status_OK;
	public static String status_readOnly;
	public static String status_targetException;
	public static String status_updateConflict;

	public static String element_doesNotExist;
	public static String element_reconciling;
	public static String element_attachingSource;
	public static String element_invalidResourceForProject;
	public static String element_nullName;
	public static String element_nullType;
	public static String element_illegalParent;

	public static String operation_notSupported;
	public static String operation_cancelled;
	public static String operation_copyResourceProgress;
	public static String operation_renameResourceProgress;
	public static String operation_moveResourceProgress;
	public static String operation_needElements;
	public static String operation_needName;
	public static String operation_needPath;
	public static String operation_needAbsolutePath;
	public static String operation_needString;
	public static String operation_nullContainer;
	public static String operation_nullName;
	public static String operation_copyElementProgress;
	public static String operation_moveElementProgress;
	public static String operation_renameElementProgress;
	public static String operation_createUnitProgress;
	public static String operation_createFieldProgress;
	public static String operation_createImportsProgress;
	public static String operation_createInitializerProgress;
	public static String operation_createMethodProgress;
	public static String operation_createFolderProgress;
	public static String operation_createTypeProgress;
	public static String operation_deleteElementProgress;
	public static String operation_deleteResourceProgress;
	public static String operation_pathOutsideProject;
	public static String operation_sortelements;

	public static String engine_searching;

	public static String Resources_outOfSyncResources;
	public static String Resources_outOfSync;
	public static String Resources_modifiedResources;
	public static String Resources_fileModified;

	public static String Change_is_unsaved;
	public static String Change_is_read_only;
	public static String Change_same_read_only;
	public static String Change_has_modifications;
	public static String Change_does_not_exist;
	public static String Change_blChanges;

	public static String DynamicValidationStateChange_workspace_changed;

	public static String RenameResourceProcessor_name;

	public static String AbstractRenameChange_Renaming;

	public static String RenameResourceRefactoring_Internal_Error;
	public static String RenameResourceRefactoring_already_exists;
	public static String RenameResourceRefactoring_invalidName;

	public static String RenameResourceChange_name;
	public static String RenameResourceChange_does_not_exist;
	public static String RenameResourceChange_rename_resource;
	public static String RenameResourceChange_descriptor_description;
	public static String RenameResourceChange_descriptor_description_short;

	public static String RenameElementAction_name;
	public static String RenameElementAction_exception;
	public static String RenameElementAction_not_available;

	public static String ActionUtil_not_possible;
	public static String ActionUtil_no_linked;

	public static String RenameSupport_not_available;
	public static String RenameSupport_dialog_title;

	public static String RenameAction_text;
	public static String RenameAction_rename;
	public static String RenameAction_unavailable;

	public static String ChangeTypeAction_dialog_title;

	public static String ExceptionDialog_seeErrorLogMessage;

	public static String RefactoringStarter_always_save;
	public static String RefactoringStarter_unexpected_exception;
	public static String RefactoringStarter_saving;
	public static String RefactoringStarter_save_all_resources;
	public static String RefactoringStarter_must_save;

	public static String QualifiedNameComponent_patterns_label;
	public static String QualifiedNameComponent_patterns_description;

	public static String RenameInputWizardPage_update_references;
	public static String RenameInputWizardPage_update_textual_matches;

	public static String RenameInputWizardPage_new_name;

	public static String RenameRefactoringWizard_internal_error;

	public static String RenameResourceWizard_defaultPageTitle;
	public static String RenameResourceWizard_inputPage_description;

	public static String RenameTypeRefactoring_name;
	public static String RenameTypeRefactoring_choose_another_name;
	public static String RenameTypeRefactoring_checking;
	public static String RenameTypeRefactoring_searching;
	public static String RenameTypeRefactoring_will_not_rename;
	public static String RenameTypeRefactoring_imported;
	public static String RenameTypeRefactoring_exists;
	public static String RenameTypeRefactoring_creating_change;
	public static String RenameTypeRefactoring_update_reference;
	public static String RenameTypeRefactoring_another_type;
	public static String RenameTypeRefactoring_name_conflict1;
	public static String RenameTypeRefactoring_searching_text;
	public static String RenameTypeRefactoring_update;
	public static String RenameTypeRefactoring_does_not_exist;
	public static String RenameTypeRefactoring_local_type;
	public static String RenameTypeRefactoring_member_type;

	public static String RenameTypeProcessor_descriptor_description;
	public static String RenameTypeProcessor_descriptor_description_short;
	public static String RenameTypeProcessor_creating_changes;
	public static String RenameTypeProcessor_change_name;
	public static String RenameTypeProcessor_checking_similarly_named_declarations_refactoring_conditions;

	public static String RenameTypeWizard_unexpected_exception;
	public static String RenameTypeWizardInputPage_description;
	public static String RenameTypeWizard_defaultPageTitle;
	public static String RenameTypeWizard_inputPage_description;

	public static String convention_unit_nullName;
	public static String convention_unit_notBlName;
	public static String convention_illegalIdentifier;
	public static String convention_import_nullImport;
	public static String convention_type_nullName;
	public static String convention_type_nameWithBlanks;
	public static String convention_type_lowercaseName;
	public static String convention_type_invalidName;

	public static String convention_folder_nullName;
	public static String convention_folder_emptyName;
	public static String convention_folder_dotName;
	public static String convention_folder_nameWithBlanks;
	public static String convention_folder_consecutiveDotsName;
	public static String convention_folder_uppercaseName;

	public static String Checks_method_names_lowercase;
	public static String Checks_no_dot;
	public static String Checks_cu_name_used;
	public static String Checks_method_native;
	public static String Checks_methodName_exists;
	public static String Checks_Choose_name;
	public static String Checks_cu_not_created;
	public static String Checks_cu_not_parsed;
	public static String Checks_cu_has_compile_errors;
	public static String Checks_validateEdit;
	public static String Checks_cannot_be_parsed;
	public static String Checks_all_excluded;
	public static String Checks_type_native;

	public static String Refactoring_not_in_model;
	public static String Refactoring_read_only;
	public static String Refactoring_unknown_structure;

	public static String ExtractTempRefactoring_convention;
	public static String ExtractConstantRefactoring_convention;

	public static String RenameEnumConstRefactoring_convention;

	public static String UndoCompilationUnitChange_no_resource;

	public static String RenameCompilationUnitRefactoring_name;
	public static String RenameCompilationUnitRefactoring_not_parsed;
	public static String RenameCompilationUnitRefactoring_not_parsed_1;
	public static String RenameCompilationUnitChange_descriptor_description;
	public static String RenameCompilationUnitChange_descriptor_description_short;
	public static String RenameCompilationUnitRefactoring_same_name;

	public static String RenameCompilationUnitChange_name;

	public static String RenameFolderChange_checking_change;
	public static String RenameFolderChange_name;
	public static String RenameFolderRefactoring_name;
	public static String RenameFolderRefactoring_creating_change;
	public static String RenameFolderRefactoring_another_name;
	public static String RenameFolderRefactoring_checking;
	public static String RenameFolderRefactoring_resource_read_only;
	public static String RenameFolderRefactoring_folder_exists;
	public static String RenameFolderRefactoring_aleady_exists;
	public static String RenameFolderRefactoring_contains_type;
	public static String RenameFolderProcessor_descriptor_description;
	public static String RenameFolderProcessor_descriptor_description_short;
	public static String RenameFolderRefactoring_change_name;
	public static String RenameFolderRefactoring_searching;

	public static String RenameFolderWizard_defaultPageTitle;
	public static String RenameFolderWizard_inputPage_description;

	public static String RenameCompilationUnitWizard_defaultPageTitle;
	public static String RenameCompilationUnitWizard_inputPage_description;

	public static String RefactoringExecutionHelper_cannot_execute;

	public static String ProcessorBasedRefactoring_error_unsupported_initialization;

	public static String InitializableRefactoring_argument_not_exist;
	public static String InitializableRefactoring_input_not_exists;
	public static String InitializableRefactoring_inputs_do_not_exist;
	public static String InitializableRefactoring_illegal_argument;
	public static String InitializableRefactoring_inacceptable_arguments;

	public static String RefactoringDescriptor_initialization_error;
	public static String RefactoringDescriptor_update_references;
	public static String RefactoringDescriptor_rename_similar;
	public static String RefactoringDescriptor_qualified_names;
	public static String RefactoringDescriptor_keep_original;
	public static String RefactoringDescriptor_not_available;
	public static String RefactoringDescriptor_rename_similar_suffix;
	public static String RefactoringDescriptor_textual_occurrences;
	public static String RefactoringDescriptor_inferred_setting_pattern;
	public static String RefactoringDescriptor_original_element_pattern;
	public static String RefactoringDescriptor_original_elements;
	public static String RefactoringDescriptor_renamed_element_pattern;
	public static String RefactoringDescriptor_rename_similar_embedded;
	public static String RefactoringDescriptor_qualified_names_pattern;
	public static String RefactoringDescriptor_keep_original_deprecated;
	public static String RefactoringDescriptorComment_element_delimiter;

	public static String LanguageElementLabels_anonym_type;
	public static String LanguageElementLabels_anonym;
	public static String LanguageElementLabels_category;
	public static String LanguageElementLabels_concat_string;
	public static String LanguageElementLabels_comma_string;
	public static String LanguageElementLabels_declseparator_string;
	public static String LanguageElementLabels_category_separator_string;
	public static String LanguageElementLabels_import_container;
	public static String LanguageElementLabels_initializer;

	public static String QualifiedNameSearchResult_change_name;

	public static String DelegateCreator_deprecate_delegates;

	public static String ReorgPolicyFactory_noCopying;
	public static String ReorgPolicyFactory_noMoving;
	public static String ReorgPolicyFactory_doesnotexist0;
	public static String ReorgPolicyFactory_readonly;
	public static String ReorgPolicyFactory_structure;
	public static String ReorgPolicyFactory_cannot;
	public static String ReorgPolicyFactory_cannot1;
	public static String ReorgPolicyFactory_not_this_resource;
	public static String ReorgPolicyFactory_linked;
	public static String ReorgPolicyFactory_workspace;
	public static String ReorgPolicyFactory_phantom;
	public static String ReorgPolicyFactory_inaccessible;
	public static String ReorgPolicyFactory_move_folders;
	public static String ReorgPolicyFactory_move_files;
	public static String ReorgPolicyFactory_move_folder;
	public static String ReorgPolicyFactory_move_file;
	public static String ReorgPolicyFactory_move_compilation_units;
	public static String ReorgPolicyFactory_move_description_plural;
	public static String ReorgPolicyFactory_move_compilation_unit;
	public static String ReorgPolicyFactory_move_description_singular;
	public static String ReorgPolicyFactory_move_header;
	public static String ReorgPolicyFactory_parent;
	public static String ReorgPolicyFactory_folders;
	public static String ReorgPolicyFactory_element2parent;
	public static String ReorgPolicyFactory_folder2parent;
	public static String ReorgPolicyFactory_folder2itself;
	public static String ReorgPolicyFactory_move_folders_plural;
	public static String ReorgPolicyFactory_move_folders_singular;
	public static String ReorgPolicyFactory_move_folders_header;
	public static String ReorgPolicyFactory_copy_folders_plural;
	public static String ReorgPolicyFactory_copy_folder_singular;
	public static String ReorgPolicyFactory_copy_folders_header;

	public static String ReorgPolicyFactory_copy_folders;
	public static String ReorgPolicyFactory_copy_files;
	public static String ReorgPolicyFactory_copy_compilation_units;
	public static String ReorgPolicyFactory_copy_description_plural;

	public static String ReorgPolicyFactory_copy_folder;
	public static String ReorgPolicyFactory_copy_file;
	public static String ReorgPolicyFactory_copy_compilation_unit;
	public static String ReorgPolicyFactory_copy_description_singular;
	public static String ReorgPolicyFactory_copy_header;

	public static String ReorgPolicy_copy;
	public static String ReorgPolicy_copy_folder;
	public static String ReorgPolicy_move;
	public static String ReorgPolicy_move_folder;

	public static String ReorgMoveAction_3;
	public static String ReorgMoveAction_4;

	public static String ReorgUserInputPage_choose_destination_single;
	public static String ReorgUserInputPage_choose_destination_multi;

	public static String ReorgMoveWizard_newFolder;
	public static String ReorgMoveWizard_3;
	public static String ReorgMoveWizard_4;
	public static String ReorgMoveWizard_textual_move;

	public static String ReorgQueries_skip_all;

	public static String MoveAction_update_references;
	public static String MoveAction_text;

	public static String MoveRefactoring_update_imports;

	public static String OpenRefactoringWizardAction_refactoring;
	public static String OpenRefactoringWizardAction_exception;

	public static String CopyProcessor_changeName;
	public static String MoveProcessor_change_name;
	public static String CopyProcessor_processorName;

	public static String MoveRefactoring_0;
	public static String MoveRefactoring_scanning_qualified_names;
	public static String MoveRefactoring_reorganize_elements;
	public static String MoveCompilationUnitChange_name;

	public static String MoveFolderChange_move;
	public static String MoveResourceChange_move;

	public static String CopyRefactoring_cu_copyOf1;
	public static String CopyRefactoring_cu_copyOfMore;
	public static String CopyRefactoring_resource_copyOf1;
	public static String CopyRefactoring_resource_copyOfMore;
	public static String CopyRefactoring_folder_copyOf1;
	public static String CopyRefactoring_folder_copyOfMore;

	public static String CopyCompilationUnitChange_copy;
	public static String CopyFolderChange_copy;
	public static String CopyResourceString_copy;

	public static String BuildPathsBlock_operationdesc_project;

	public static String NewContainerWizardPage_container_label;
	public static String NewContainerWizardPage_container_button;
	public static String NewContainerWizardPage_error_EnterContainerName;

	public static String NewContainerWizardPage_error_ContainerDoesNotExist;
	public static String NewContainerWizardPage_error_NotAFolder;
	public static String NewContainerWizardPage_error_ProjectClosed;

	public static String NewContainerWizardPage_warning_NotAZ8Project;
	public static String NewContainerWizardPage_warning_NotInAZ8Project;

	public static String NewContainerWizardPage_ChooseSourceContainerDialog_title;
	public static String NewContainerWizardPage_ChooseSourceContainerDialog_description;

	public static String NewFolderWizardPage_title;
	public static String NewFolderWizardPage_description;
	public static String NewFolderWizardPage_info;
	public static String NewFolderWizardPage_folder_label;

	public static String NewFolderCreationWizard_title;

	public static String NewFolderWizardPage_error_InvalidFolderName;
	public static String NewFolderWizardPage_error_IsOutputFolder;

	public static String NewFolderWizardPage_error_FolderExists;
	public static String NewFolderWizardPage_error_FolderExistsDifferentCase;
	public static String NewFolderWizardPage_error_EnterName;
	public static String NewFolderWizardPage_error_FolderNotShown;
	public static String NewFolderWizardPage_warning_DiscouragedFolderName;

	public static String NewElementWizard_op_error_title;
	public static String NewElementWizard_op_error_message;

	public static String OverwriteHelper_0;
	public static String OverwriteHelper_1;

	public static String OverwriteHelper_2;
	public static String OverwriteHelper_3;

	static {
		NLS.initializeMessages(BUNDLE_NAME, RefactoringMessages.class);
	}
}