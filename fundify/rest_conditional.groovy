import com.atlassian.sal.api.component.ComponentLocator
import com.atlassian.confluence.security.Permission
import com.atlassian.confluence.security.PermissionManager

PermissionManager pman = ComponentLocator.getComponent(PermissionManager)

context.getSpace().key == "CT" && pman.hasPermission(Users.getLoggedInUser(),Permission.EDIT,context.page)
