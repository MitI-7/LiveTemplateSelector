import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateManagerImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.sun.istack.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.Map;

public class SelectorAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();

        TemplateManager templateManager = TemplateManager.getInstance(project);
        TemplateSettings templateSetting = TemplateSettings.getInstance();
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();

        // template選択画面
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        TemplateActionContext templateActionContext = TemplateActionContext.create(psiFile, editor, 0, 0, false);

        List<TemplateImpl> templates = TemplateManagerImpl.listApplicableTemplateWithInsertingDummyIdentifier(templateActionContext);
        SelectLiveTemplate form = new SelectLiveTemplate(makeModel(templates), templateManager, templateSetting, editor);

        CaretModel caretModel = editor.getCaretModel();
        Point cursorAbsoluteLocation = editor.visualPositionToXY(caretModel.getVisualPosition());
        Point editorLocation = editor.getComponent().getLocationOnScreen();
        Point editorContentLocation = editor.getContentComponent().getLocationOnScreen();
        Point popupLocation = new Point(editorContentLocation.x + cursorAbsoluteLocation.x - 10,
                editorLocation.y + cursorAbsoluteLocation.y - editor.getScrollingModel().getVerticalScrollOffset() + 13);

        form.setVisible(true);
        form.setLocation(popupLocation);
    }

    // 表示するLiveTemplateのデータを作成
    DefaultMutableTreeNode makeModel(@NotNull List<TemplateImpl> templateList) {
        // Template Group -> Live Templateのリストのmapを作成
        Map<String, List<String>> map = new HashMap<>();
        templateList.forEach((t) -> {
            String key = t.getKey();
            String groupName = t.getGroupName();

            map.putIfAbsent(groupName, new ArrayList<>());
            map.get(groupName).add(key);
        });

        // Template GroupごとのNodeを作成しrootNodeにいれてく
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Live Template Group");
        map.forEach((groupName, key_list) -> {
            DefaultMutableTreeNode templateGroup = new DefaultMutableTreeNode(groupName);
            key_list.forEach((key) -> {
                templateGroup.add(new DefaultMutableTreeNode(key));
            });
            rootNode.add(templateGroup);
        });
        return rootNode;
    }
}
